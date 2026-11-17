import type { components } from '../../shared/api/schema'

type SpaceType = components['schemas']['SpaceType']

export const SPACE_TYPE_LABELS: Record<SpaceType, string> = {
  ROOM: 'Sala',
  SHARED_TABLE: 'Mesa compartilhada',
  WORKSTATION: 'Estação individual',
}
