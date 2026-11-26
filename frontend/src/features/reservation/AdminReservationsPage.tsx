import { useRef, useState } from 'react'
import { ListPage } from '../../shared/ui/ListPage.tsx'
import { Notice } from '../../shared/ui/Notice.tsx'
import { PagedList } from '../../shared/ui/PagedList.tsx'
import { useAdminAccount } from '../admin-auth/adminAccount.ts'
import { AdminReservationFilters } from './AdminReservationFilters.tsx'
import { AdminReservationItem } from './AdminReservationItem.tsx'
import styles from './AdminReservationPage.module.css'
import {
  useAdminReservations,
  type ReservationFilters,
} from './adminReservationsApi.ts'

export function AdminReservationsPage() {
  const account = useAdminAccount()
  const allowed = account.permissions.includes('MANAGE_SPACES')
  const [page, setPage] = useState(0)
  const [filters, setFilters] = useState<ReservationFilters>({})
  const reservations = useAdminReservations(filters, page, allowed)
  const filterPanel = useRef<HTMLDetailsElement>(null)
  const activeFilters = Object.values(filters).filter(Boolean).length

  function applyFilters(next: ReservationFilters) {
    setFilters(next)
    setPage(0)
    const panel = filterPanel.current
    if (panel?.open) {
      panel.open = false
      panel.querySelector('summary')?.focus()
    }
  }

  return (
    <ListPage title="Reservas">
      {allowed ? (
        <>
          <details ref={filterPanel} className={styles.filterPanel}>
            <summary>
              Filtrar reservas
              {activeFilters > 0 && ` (${activeFilters})`}
            </summary>
            <AdminReservationFilters onApply={applyFilters} />
          </details>
          <PagedList
            data={reservations.data}
            error={reservations.error}
            page={page}
            onPageChange={setPage}
            emptyText="Nenhum pedido encontrado."
          >
            {(entry) => (
              <AdminReservationItem key={entry.protocol} entry={entry} />
            )}
          </PagedList>
        </>
      ) : (
        <Notice tone="info">
          Sua conta não tem acesso às solicitações de reserva.
        </Notice>
      )}
    </ListPage>
  )
}
