import {
  ADMIN_HOME,
  ADMIN_PASSWORD,
} from '../features/admin-auth/adminRoutes.ts'
import { ADMIN_SPACES } from '../features/admin-spaces/spaceRoutes.ts'
import { ADMIN_TEAM } from '../features/admin-team/teamRoutes.ts'
import { ADMIN_RESERVATIONS } from '../features/reservation/reservationRoutes.ts'
import type { components } from '../shared/api/schema'

export type AdminAccount = components['schemas']['AdminAccount']

type MenuItem = {
  to: string
  label: string
  description: string
  permission?: AdminAccount['permissions'][number]
}

const MENU: MenuItem[] = [
  {
    to: ADMIN_HOME,
    label: 'Visão geral',
    description: 'Atalhos para as áreas da administração.',
  },
  {
    to: ADMIN_RESERVATIONS,
    label: 'Reservas',
    description: 'Solicitações recebidas e decisões da equipe.',
  },
  {
    to: ADMIN_SPACES,
    label: 'Espaços',
    description: 'Salas, mesas e estações disponíveis para reserva.',
    permission: 'MANAGE_SPACES',
  },
  {
    to: ADMIN_TEAM,
    label: 'Equipe',
    description: 'Funcionários, permissões e histórico de acesso.',
    permission: 'MANAGE_ACCESS',
  },
  {
    to: ADMIN_PASSWORD,
    label: 'Alterar senha',
    description: 'Troque a senha da sua conta.',
  },
]

export function adminMenuFor(account: AdminAccount) {
  return MENU.filter(
    (item) => !item.permission || account.permissions.includes(item.permission),
  )
}
