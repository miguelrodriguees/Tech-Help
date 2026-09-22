package br.com.techhelp.service;

import br.com.techhelp.dto.LocacaoDtos.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.util.*;

/** Fluxo autenticado de aluguel. As rotas antigas permanecem bloqueadas. */
@Service
public class LocacaoService {
    private final JdbcTemplate db;
    public LocacaoService(JdbcTemplate db) { this.db=db; }
    private static long number(Object value) { return ((Number)value).longValue(); }
    private static BigDecimal money(Object value) { return value==null?null:new BigDecimal(value.toString()); }
    private static LocalDateTime date(Object value) { return value instanceof Timestamp t?t.toLocalDateTime():(LocalDateTime)value; }
    private static String hash(Pedido p) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(p.toString().getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    private Map<String,Object> row(Long id, boolean lock) {
        var rows=db.queryForList("SELECT * FROM aluguel WHERE id_aluguel=?"+(lock?" FOR UPDATE":""),id);
        if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Aluguel não encontrado");
        var r=rows.getFirst();
        if(r.get("chave_pedido")==null)throw new ResponseStatusException(HttpStatus.CONFLICT,"Aluguel legado: requer revisão antes de operar neste módulo");
        return r;
    }
    private void owner(Map<String,Object> r, Long user) {
        if(number(r.get("id_usuario"))!=user)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Aluguel não encontrado");
    }
    private List<Map<String,Object>> composition(long kit) {
        return db.queryForList("SELECT id_ferramenta,quantidade FROM kit_ferramenta WHERE id_kit=? ORDER BY id_ferramenta",kit);
    }
    public List<Map<String,Object>> catalog() {
        var tools=db.queryForList("SELECT * FROM ferramenta ORDER BY nome");
        Map<Long,Map<String,Object>> byId=new HashMap<>();
        List<Map<String,Object>> catalog=new ArrayList<>();
        for(var t:tools){
            long id=number(t.get("id_ferramenta"));byId.put(id,t);
            if(!"INATIVA".equals(t.get("status")))catalog.add(catalogItem("FERRAMENTA",id,t,
                "DISPONIVEL".equals(t.get("status"))?number(t.get("quantidade_disponivel")):0,List.of()));
        }
        for(var k:db.queryForList("SELECT * FROM kit WHERE status<>'INATIVO' ORDER BY nome")){
            var parts=composition(number(k.get("id_kit")));
            long available=parts.isEmpty()||!"DISPONIVEL".equals(k.get("status"))?0:Long.MAX_VALUE;
            List<String> descriptions=new ArrayList<>();
            for(var part:parts){
                var t=byId.get(number(part.get("id_ferramenta")));
                long qty=number(part.get("quantidade"));
                available=Math.min(available,"DISPONIVEL".equals(t.get("status"))?number(t.get("quantidade_disponivel"))/qty:0);
                descriptions.add(qty+" × "+t.get("nome"));
            }
            catalog.add(catalogItem("KIT",number(k.get("id_kit")),k,available,descriptions));
        }
        return catalog;
    }
    private Map<String,Object> catalogItem(String type,long id,Map<String,Object> r,long qty,List<String> parts){
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("tipo",type);result.put("id",id);result.put("nome",r.get("nome"));result.put("descricao",r.get("descricao"));
        result.put("diaria",r.get("valor_diaria"));result.put("disponivel",qty);result.put("componentes",parts);return result;
    }
    @Transactional
    public Map<String,Object> create(Long user,Pedido p){
        // Uma chave por intenção de compra: reenvio não cria outro aluguel.
        db.queryForList("SELECT id_usuario FROM usuario WHERE id_usuario=? FOR UPDATE",user);
        var prior=db.queryForList("SELECT id_aluguel,pedido_hash FROM aluguel WHERE id_usuario=? AND chave_pedido=?",user,p.chave());
        if(!prior.isEmpty()){
            if(!hash(p).equals(prior.getFirst().get("pedido_hash")))throw new IllegalArgumentException("Esta chave já foi usada para outro pedido");
            return detail(number(prior.getFirst().get("id_aluguel")));
        }
        long days=LocacaoRegras.diarias(p.retirada(),p.devolucao(),LocalDateTime.now());
        boolean delivery="ENTREGA".equals(p.recebimento()),pickup="COLETA".equals(p.retorno());
        boolean transport=delivery||pickup;
        if(transport&&(p.endereco()==null||p.endereco().trim().length()<15))throw new IllegalArgumentException("Informe rua, número, bairro, cidade/UF e CEP para o transporte");
        TreeMap<Long,Integer> consumption=new TreeMap<>();
        Set<String> selected=new HashSet<>();
        List<BigDecimal> prices=new ArrayList<>();BigDecimal rental=BigDecimal.ZERO;
        for(Item item:p.itens()){
            if(!selected.add(item.tipo()+item.id()))throw new IllegalArgumentException("Item repetido no pedido");
            boolean kit="KIT".equals(item.tipo());
            var rows=db.queryForList(kit?"SELECT * FROM kit WHERE id_kit=?":"SELECT * FROM ferramenta WHERE id_ferramenta=?",item.id());
            if(rows.isEmpty()||!"DISPONIVEL".equals(rows.getFirst().get("status")))throw new IllegalArgumentException("Um item não está disponível no catálogo");
            BigDecimal price=money(rows.getFirst().get("valor_diaria"));prices.add(price);
            rental=rental.add(price.multiply(BigDecimal.valueOf(days*item.quantidade())));
            if(kit){
                var parts=composition(item.id());if(parts.isEmpty())throw new IllegalArgumentException("Kit sem ferramentas não pode ser alugado");
                for(var part:parts)consumption.merge(number(part.get("id_ferramenta")),Math.toIntExact(number(part.get("quantidade"))*item.quantidade()),Math::addExact);
            }else consumption.merge(item.id(),item.quantidade(),Math::addExact);
        }
        limit(rental);
        db.update("INSERT INTO aluguel(id_usuario,data_prevista_retirada,data_prevista_devolucao,valor_total,status,tipo_recebimento,tipo_devolucao,endereco_transporte,taxa_entrega,taxa_coleta,chave_pedido,pedido_hash) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
            user,p.retirada(),p.devolucao(),rental,transport?"AGUARDANDO_TAXA":"AGUARDANDO_ACEITE",p.recebimento(),p.retorno(),transport?p.endereco().trim():null,delivery?null:BigDecimal.ZERO,pickup?null:BigDecimal.ZERO,p.chave(),hash(p));
        Long id=db.queryForObject("SELECT id_aluguel FROM aluguel WHERE id_usuario=? AND chave_pedido=?",Long.class,user,p.chave());
        for(int i=0;i<p.itens().size();i++){
            Item item=p.itens().get(i);
            db.update("INSERT INTO aluguel_item(id_aluguel,id_kit,id_ferramenta,quantidade,valor_diaria) VALUES(?,?,?,?,?)",id,"KIT".equals(item.tipo())?item.id():null,"FERRAMENTA".equals(item.tipo())?item.id():null,item.quantidade(),prices.get(i));
        }
        for(var entry:consumption.entrySet())db.update("INSERT INTO aluguel_consumo(id_aluguel,id_ferramenta,quantidade) VALUES(?,?,?)",id,entry.getKey(),entry.getValue());
        if(!transport)reserve(row(id,true));
        return detail(id);
    }
    private void limit(BigDecimal value){if(value.compareTo(new BigDecimal("9999999999.99"))>0)throw new IllegalArgumentException("Valor acima do limite permitido");}
    public List<Map<String,Object>> mine(Long user){return db.queryForList("SELECT id_aluguel FROM aluguel WHERE id_usuario=? AND chave_pedido IS NOT NULL ORDER BY id_aluguel DESC LIMIT 100",user).stream().map(r->detail(number(r.get("id_aluguel")))).toList();}
    public List<Map<String,Object>> operations(){return db.queryForList("SELECT id_aluguel FROM aluguel WHERE chave_pedido IS NOT NULL ORDER BY id_aluguel DESC LIMIT 100").stream().map(r->detail(number(r.get("id_aluguel")))).toList();}
    public Map<String,Object> detail(Long id){
        var r=row(id,false);Map<String,Object> result=new LinkedHashMap<>();
        result.put("id",id);result.put("status",r.get("status"));result.put("recebimento",r.get("tipo_recebimento"));result.put("retorno",r.get("tipo_devolucao"));
        result.put("retirada",date(r.get("data_prevista_retirada")).toString());result.put("devolucao",date(r.get("data_prevista_devolucao")).toString());
        result.put("endereco",r.get("endereco_transporte"));result.put("observacao",r.get("observacao_transporte"));
        result.put("aluguel",r.get("valor_total"));result.put("entrega",r.get("taxa_entrega"));result.put("coleta",r.get("taxa_coleta"));
        result.put("total",LocacaoRegras.total(money(r.get("valor_total")),money(r.get("taxa_entrega")),money(r.get("taxa_coleta"))));
        result.put("usuario",db.queryForObject("SELECT nome FROM usuario WHERE id_usuario=?",String.class,r.get("id_usuario")));
        result.put("itens",db.queryForList("SELECT COALESCE(k.nome,f.nome) AS nome,i.quantidade,i.valor_diaria AS diaria FROM aluguel_item i LEFT JOIN kit k ON k.id_kit=i.id_kit LEFT JOIN ferramenta f ON f.id_ferramenta=i.id_ferramenta WHERE i.id_aluguel=? ORDER BY i.id_aluguel_item",id));
        return result;
    }
    @Transactional
    public Map<String,Object> quote(Long id,Taxas fees){
        var r=row(id,true);
        if(!"AGUARDANDO_TAXA".equals(r.get("status")))throw new IllegalArgumentException("Este pedido não está aguardando cotação");
        if(!"ENTREGA".equals(r.get("tipo_recebimento"))&&fees.entrega().signum()!=0||!"COLETA".equals(r.get("tipo_devolucao"))&&fees.coleta().signum()!=0)throw new IllegalArgumentException("Não cobre transporte que não foi solicitado");
        limit(LocacaoRegras.total(money(r.get("valor_total")),fees.entrega(),fees.coleta()));
        db.update("UPDATE aluguel SET taxa_entrega=?,taxa_coleta=?,observacao_transporte=?,status='AGUARDANDO_ACEITE' WHERE id_aluguel=?",fees.entrega(),fees.coleta(),fees.observacao().trim(),id);
        return detail(id);
    }
    @Transactional
    public Map<String,Object> accept(Long user,Long id){
        var r=row(id,true);owner(r,user);
        if("RESERVADO".equals(r.get("status")))return detail(id);
        if(!"AGUARDANDO_ACEITE".equals(r.get("status")))throw new IllegalArgumentException("Aguarde a taxa antes de confirmar");
        reserve(r);return detail(id);
    }
    private List<Map<String,Object>> consumption(Long id){return db.queryForList("SELECT id_ferramenta,quantidade FROM aluguel_consumo WHERE id_aluguel=? ORDER BY id_ferramenta",id);}
    private Map<String,Object> lockTool(long id){
        var rows=db.queryForList("SELECT * FROM ferramenta WHERE id_ferramenta=? FOR UPDATE",id);
        if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Ferramenta não encontrada");
        return rows.getFirst();
    }
    private void reserve(Map<String,Object> r){
        Long id=number(r.get("id_aluguel"));
        LocacaoRegras.diarias(date(r.get("data_prevista_retirada")),date(r.get("data_prevista_devolucao")),LocalDateTime.now());
        var parts=consumption(id);if(parts.isEmpty())throw new IllegalArgumentException("Pedido sem equipamentos");
        // Ordem estável dos locks evita reservas simultâneas excederem o estoque.
        for(var part:parts){
            var t=lockTool(number(part.get("id_ferramenta")));
            if(!"DISPONIVEL".equals(t.get("status"))||number(t.get("quantidade_disponivel"))<number(part.get("quantidade")))throw new IllegalArgumentException("Estoque insuficiente para "+t.get("nome")+". O pedido ainda não foi confirmado.");
        }
        for(var part:parts)db.update("UPDATE ferramenta SET quantidade_disponivel=quantidade_disponivel-? WHERE id_ferramenta=?",part.get("quantidade"),part.get("id_ferramenta"));
        db.update("UPDATE aluguel SET status='RESERVADO',estoque_reservado=TRUE WHERE id_aluguel=?",id);
    }
    @Transactional
    public Map<String,Object> cancel(Long user,Long id,boolean admin){
        var r=row(id,true);if(!admin)owner(r,user);
        String status=r.get("status").toString();if("CANCELADO".equals(status))return detail(id);
        if(!Set.of("AGUARDANDO_TAXA","AGUARDANDO_ACEITE","RESERVADO").contains(status))throw new IllegalArgumentException("Só é possível cancelar antes da entrega ou retirada");
        if("RESERVADO".equals(status))release(id);
        db.update("UPDATE aluguel SET status='CANCELADO',estoque_reservado=FALSE WHERE id_aluguel=?",id);return detail(id);
    }
    @Transactional
    public Map<String,Object> handover(Long id){
        var r=row(id,true);if("RETIRADO".equals(r.get("status")))return detail(id);
        if(!"RESERVADO".equals(r.get("status")))throw new IllegalArgumentException("Confirme a reserva antes de registrar a entrega ou retirada");
        for(var part:consumption(id))if(!"DISPONIVEL".equals(lockTool(number(part.get("id_ferramenta"))).get("status")))throw new IllegalArgumentException("Há equipamento em manutenção ou inativo. Não entregue o pedido.");
        db.update("UPDATE aluguel SET status='RETIRADO',data_retirada=CURRENT_TIMESTAMP(6) WHERE id_aluguel=?",id);return detail(id);
    }
    @Transactional
    public Map<String,Object> returned(Long id){
        var r=row(id,true);if("DEVOLVIDO".equals(r.get("status")))return detail(id);
        if(!Set.of("RETIRADO","ATRASADO").contains(r.get("status")))throw new IllegalArgumentException("Este pedido ainda não foi entregue ou retirado");
        release(id);db.update("UPDATE aluguel SET status='DEVOLVIDO',data_devolucao=CURRENT_TIMESTAMP(6),estoque_reservado=FALSE WHERE id_aluguel=?",id);return detail(id);
    }
    private void release(Long id){
        for(var part:consumption(id)){
            lockTool(number(part.get("id_ferramenta")));
            db.update("UPDATE ferramenta SET quantidade_disponivel=quantidade_disponivel+? WHERE id_ferramenta=?",part.get("quantidade"),part.get("id_ferramenta"));
        }
    }
    public List<Map<String,Object>> inventory(){return db.queryForList("SELECT id_ferramenta AS id,nome,descricao,quantidade_total AS quantidade,quantidade_disponivel AS disponivel,valor_diaria AS diaria,status FROM ferramenta ORDER BY nome");}
    @Transactional
    public void addTool(Ferramenta t){
        if(db.queryForObject("SELECT COUNT(*) FROM ferramenta WHERE nome=?",Long.class,t.nome().trim())>0)throw new IllegalArgumentException("Já existe ferramenta com esse nome");
        db.update("INSERT INTO ferramenta(nome,descricao,quantidade_total,quantidade_disponivel,valor_diaria,status) VALUES(?,?,?,?,?,'DISPONIVEL')",t.nome().trim(),t.descricao(),t.quantidade(),t.quantidade(),t.diaria());
    }
    @Transactional
    public void toolStatus(Long id,String status){lockTool(id);db.update("UPDATE ferramenta SET status=? WHERE id_ferramenta=?",status,id);}
    @Transactional
    public void addKit(Kit k){
        if(db.queryForObject("SELECT COUNT(*) FROM kit WHERE nome=?",Long.class,k.nome().trim())>0)throw new IllegalArgumentException("Já existe kit com esse nome");
        Set<Long> seen=new HashSet<>();
        for(var c:k.componentes()){
            if(!seen.add(c.id()))throw new IllegalArgumentException("Ferramenta repetida no kit");
            var rows=db.queryForList("SELECT quantidade_total FROM ferramenta WHERE id_ferramenta=?",c.id());
            if(rows.isEmpty()||number(rows.getFirst().get("quantidade_total"))<c.quantidade())throw new IllegalArgumentException("Composição excede o estoque total de uma ferramenta");
        }
        db.update("INSERT INTO kit(nome,descricao,valor_diaria,status) VALUES(?,?,?,'DISPONIVEL')",k.nome().trim(),k.descricao(),k.diaria());
        Long id=db.queryForObject("SELECT id_kit FROM kit WHERE nome=?",Long.class,k.nome().trim());
        for(var c:k.componentes())db.update("INSERT INTO kit_ferramenta(id_kit,id_ferramenta,quantidade) VALUES(?,?,?)",id,c.id(),c.quantidade());
    }
}
