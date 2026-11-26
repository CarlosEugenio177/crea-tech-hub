import { Link } from 'react-router'
import { useAdminAccount } from '../features/admin-auth/adminAccount.ts'
import {
  ADMIN_HOME,
  ADMIN_PASSWORD,
} from '../features/admin-auth/adminRoutes.ts'
import { useAdminReservations } from '../features/reservation/adminReservationsApi.ts'
import { ADMIN_RESERVATIONS } from '../features/reservation/reservationRoutes.ts'
import { ButtonLink } from '../shared/ui/ButtonLink.tsx'
import { PageHeader } from '../shared/ui/PageHeader.tsx'
import styles from './AdminHomePage.module.css'
import { adminMenuFor } from './adminMenu.ts'

export function AdminHomePage() {
  const account = useAdminAccount()
  const managesReservations = account.permissions.includes('MANAGE_SPACES')
  const pending = useAdminReservations(
    { status: 'PENDING' },
    0,
    managesReservations,
    1,
  )
  const count = pending.data?.totalElements
  const areas = adminMenuFor(account).filter(
    ({ to }) =>
      to !== ADMIN_HOME && to !== ADMIN_RESERVATIONS && to !== ADMIN_PASSWORD,
  )

  return (
    <div className={styles.page}>
      <PageHeader title="Visão geral" />
      {managesReservations && (
        <section
          className={styles.queue}
          aria-labelledby="pending-reservations"
        >
          <div>
            <h2 id="pending-reservations">Reservas</h2>
            <p className={styles.status}>
              {count === undefined
                ? pending.isError
                  ? 'Fila indisponível no momento.'
                  : 'Consultando pedidos…'
                : count === 0
                  ? 'Nenhum pedido aguardando análise.'
                  : `${count} ${count === 1 ? 'pedido aguardando' : 'pedidos aguardando'} análise.`}
            </p>
          </div>
          <ButtonLink to={ADMIN_RESERVATIONS}>
            Abrir fila de reservas
          </ButtonLink>
        </section>
      )}
      {areas.length > 0 && (
        <ul className={styles.areas} aria-label="Áreas do painel">
          {areas.map((area) => (
            <li key={area.to}>
              <Link to={area.to} className={styles.area}>
                <span>
                  <strong>{area.label}</strong>
                  <span className={styles.description}>{area.description}</span>
                </span>
                <span className={styles.arrow} aria-hidden="true">
                  →
                </span>
              </Link>
            </li>
          ))}
        </ul>
      )}
      {!managesReservations && areas.length === 0 && (
        <Link to={ADMIN_PASSWORD} className={styles.password}>
          Trocar minha senha
        </Link>
      )}
    </div>
  )
}
