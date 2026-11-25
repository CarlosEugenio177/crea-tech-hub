import { useState } from 'react'
import { ListPage } from '../../shared/ui/ListPage.tsx'
import { Notice } from '../../shared/ui/Notice.tsx'
import { PagedList } from '../../shared/ui/PagedList.tsx'
import { useAdminAccount } from '../admin-auth/adminAccount.ts'
import { AdminReservationFilters } from './AdminReservationFilters.tsx'
import { AdminReservationItem } from './AdminReservationItem.tsx'
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

  return (
    <ListPage
      eyebrow="Administração · Reservas"
      title="Reservas"
      description="Consulte os pedidos recebidos pelo site."
    >
      {allowed ? (
        <>
          <AdminReservationFilters
            onApply={(next) => {
              setFilters(next)
              setPage(0)
            }}
          />
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
