import { json } from '../../test/fakeApi.ts'

export const CATALOG = 'GET /api/v1/spaces'

export const DAY_AGENDA = `GET /api/v1/spaces/3d7c6c7f-7c5e-4e31-b6c1-8d9f3c1fca02/availability/day`

export const wholeRoomAgenda = () =>
  json({
    closed: false,
    bookingMode: 'WHOLE_SPACE',
    periods: [{ start: '08:00', end: '18:00', available: true }],
    freeWindows: [{ start: '08:00', end: '18:00' }],
    stations: [],
  })

export const catalog = () =>
  json({
    items: [
      {
        id: '3d7c6c7f-7c5e-4e31-b6c1-8d9f3c1fca01',
        name: 'Estação individual',
        type: 'WORKSTATION',
        capacity: 1,
        description: 'Seu posto com computador, reservado só para você.',
      },
      {
        id: '3d7c6c7f-7c5e-4e31-b6c1-8d9f3c1fca02',
        name: 'Sala de podcast',
        type: 'ROOM',
        capacity: 4,
        description: 'Um espaço dedicado à gravação de podcast.',
      },
      {
        id: '3d7c6c7f-7c5e-4e31-b6c1-8d9f3c1fca03',
        name: 'Mesa colaborativa',
        type: 'SHARED_TABLE',
        capacity: 8,
        description: 'Uma mesa ampla para encontros e trabalho em equipe.',
      },
    ],
    page: 0,
    size: 20,
    totalElements: 3,
    totalPages: 1,
  })
