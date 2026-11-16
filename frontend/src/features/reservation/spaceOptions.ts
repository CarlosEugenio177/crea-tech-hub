import workstation from '../../shared/assets/mulher-mesa.svg'
import sharedTable from './assets/aperto-de-maos.svg'
import podcast from './assets/podcast.svg'

export type SpaceOption = {
  id: string
  name: string
  description: string
  tag: string
  illustration: string
  illustrationAlt: string
}

// Provisório: as opções do Framer ficam fixas até existir o catálogo público (CTH-1).
export const SPACE_OPTIONS: SpaceOption[] = [
  {
    id: 'estacao-individual',
    name: 'Estação individual',
    description: 'Seu posto com computador, reservado só para você.',
    tag: '1 pessoa · Computador',
    illustration: workstation,
    illustrationAlt: 'Pessoa trabalhando em uma mesa',
  },
  {
    id: 'sala-de-podcast',
    name: 'Sala de podcast',
    description: 'Um espaço dedicado à gravação de podcast.',
    tag: 'Sala exclusiva',
    illustration: podcast,
    illustrationAlt: 'Fones de ouvido e microfone',
  },
  {
    id: 'mesa-colaborativa',
    name: 'Mesa colaborativa',
    description: 'Uma mesa ampla para encontros e trabalho em equipe.',
    tag: 'Somente para grupos',
    illustration: sharedTable,
    illustrationAlt: 'Aperto de mãos',
  },
]

export function findSpaceOption(id: string | undefined) {
  return SPACE_OPTIONS.find((option) => option.id === id)
}
