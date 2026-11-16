import {
  RESERVE,
  RESERVE_REQUESTER,
  RESERVE_REVIEW,
  RESERVE_TIME,
} from './reservationRoutes.ts'

export const STEPS = [
  { label: 'Espaço', to: RESERVE },
  { label: 'Horário', to: RESERVE_TIME },
  { label: 'Seus dados', to: RESERVE_REQUESTER },
  { label: 'Revisão', to: RESERVE_REVIEW },
]
