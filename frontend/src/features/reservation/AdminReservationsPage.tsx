import { ListPage } from '../../shared/ui/ListPage.tsx'
import { Notice } from '../../shared/ui/Notice.tsx'

export function AdminReservationsPage() {
  return (
    <ListPage
      eyebrow="Administração · Reservas"
      title="Reservas"
      description="Solicitações recebidas pelo site, para análise e decisão da equipe."
    >
      <Notice tone="info">
        A consulta de solicitações e as ações da equipe estarão disponíveis na
        próxima etapa.
      </Notice>
    </ListPage>
  )
}
