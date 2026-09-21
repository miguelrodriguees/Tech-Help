import { AppWindow, Laptop, Network, Printer, Server, Shield } from 'lucide-react';
import type { ElementType } from 'react';
export function iconFor(nome: string): ElementType {
  const value = nome.toLowerCase();
  if (value.includes('hardware')) return Laptop;
  if (value.includes('rede')) return Network;
  if (value.includes('software')) return AppWindow;
  if (value.includes('segur')) return Shield;
  if (value.includes('impress') || value.includes('perif')) return Printer;
  return Server;
}
