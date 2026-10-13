import { useEffect, useState } from 'react';
import { loadTechnicianNames, type TechnicianNames } from './technicians';

export function useTechnicianNames(ids: number[], reload: number) {
  const key = [...new Set(ids)].sort((a, b) => a - b).join(',');
  const [names, setNames] = useState<TechnicianNames>({});
  useEffect(() => {
    const controller = new AbortController();
    loadTechnicianNames(key ? key.split(',').map(Number) : [], controller.signal)
      .then(result => { if (!controller.signal.aborted) setNames(result); });
    return () => controller.abort();
  }, [key, reload]);
  return names;
}
