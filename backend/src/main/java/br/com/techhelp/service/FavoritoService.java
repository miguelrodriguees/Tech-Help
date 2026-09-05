package br.com.techhelp.service;

import br.com.techhelp.dto.CriarFavoritoRequest;
import br.com.techhelp.model.Favorito;
import br.com.techhelp.model.FavoritoId;
import br.com.techhelp.repository.ClienteRepository;
import br.com.techhelp.repository.FavoritoRepository;
import br.com.techhelp.repository.TecnicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final ClienteRepository clienteRepository;
    private final TecnicoRepository tecnicoRepository;

    public FavoritoService(
            FavoritoRepository favoritoRepository,
            ClienteRepository clienteRepository,
            TecnicoRepository tecnicoRepository
    ) {
        this.favoritoRepository = favoritoRepository;
        this.clienteRepository = clienteRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    public Favorito adicionar(
            CriarFavoritoRequest request
    ) {

        if (!clienteRepository.existsById(request.idCliente())) {
            throw new NoSuchElementException(
                    "Cliente não encontrado"
            );
        }

        if (!tecnicoRepository.existsById(request.idTecnico())) {
            throw new NoSuchElementException(
                    "Técnico não encontrado"
            );
        }

        if (favoritoRepository
                .existsByIdClienteAndIdTecnico(
                        request.idCliente(),
                        request.idTecnico()
                )) {

            throw new IllegalArgumentException(
                    "Este técnico já está nos favoritos"
            );
        }

        Favorito favorito = new Favorito();

        favorito.setIdCliente(request.idCliente());
        favorito.setIdTecnico(request.idTecnico());

        return favoritoRepository.save(favorito);
    }

    public List<Favorito> listarPorCliente(
            Long idCliente
    ) {

        if (!clienteRepository.existsById(idCliente)) {
            throw new NoSuchElementException(
                    "Cliente não encontrado"
            );
        }

        return favoritoRepository
                .findByIdClienteOrderByDataCadastroDesc(
                        idCliente
                );
    }

    public void remover(
            Long idCliente,
            Long idTecnico
    ) {

        if (!favoritoRepository
                .existsByIdClienteAndIdTecnico(
                        idCliente,
                        idTecnico
                )) {

            throw new NoSuchElementException(
                    "Favorito não encontrado"
            );
        }

        FavoritoId id =
                new FavoritoId(idCliente, idTecnico);

        favoritoRepository.deleteById(id);
    }
}