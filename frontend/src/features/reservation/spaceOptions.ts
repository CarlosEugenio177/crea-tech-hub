import workstation from '../../shared/assets/mulher-mesa.svg'
import sharedTable from './assets/aperto-de-maos.svg'
import podcast from './assets/podcast.svg'
import type { PublicSpace } from './publicSpacesApi.ts'

export type SpaceOption = {
  id: string
  name: string
  description: string
  tag: string
  illustration: string
  illustrationAlt: string
}

const presentationByType = {
  ROOM: {
    tag: 'Sala',
    illustration: podcast,
    illustrationAlt: 'Fones de ouvido e microfone',
  },
  SHARED_TABLE: {
    tag: 'Mesa colaborativa',
    illustration: sharedTable,
    illustrationAlt: 'Aperto de mãos',
  },
  WORKSTATION: {
    tag: 'Estação individual',
    illustration: workstation,
    illustrationAlt: 'Pessoa trabalhando em uma mesa',
  },
} satisfies Record<
  PublicSpace['type'],
  Pick<SpaceOption, 'tag' | 'illustration' | 'illustrationAlt'>
>

export function toSpaceOption(space: PublicSpace): SpaceOption {
  const presentation = presentationByType[space.type]
  const capacity = `${space.capacity} ${space.capacity === 1 ? 'pessoa' : 'pessoas'}`

  return {
    id: space.id,
    name: space.name,
    description: space.description ?? 'Espaço disponível para solicitação.',
    tag: `${presentation.tag} · ${capacity}`,
    illustration: presentation.illustration,
    illustrationAlt: presentation.illustrationAlt,
  }
}
