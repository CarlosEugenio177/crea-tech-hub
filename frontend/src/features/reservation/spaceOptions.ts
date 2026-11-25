import { capacityLabel } from '../../shared/format/capacityLabel.ts'
import type { PublicSpace } from './publicSpacesApi.ts'

const labels = {
  ROOM: 'Sala',
  SHARED_TABLE: 'Mesa colaborativa',
  WORKSTATION: 'Estação individual',
} satisfies Record<PublicSpace['type'], string>

export function spaceDetails(space: PublicSpace) {
  return space.bookingMode === 'PER_STATION'
    ? 'Reserva por estação'
    : `${labels[space.type]} · ${capacityLabel(space.capacity)}`
}
