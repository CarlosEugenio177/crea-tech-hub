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
        O painel de reservas ainda não está disponível. As solicitações
        aparecerão aqui quando o envio pelo site for liberado.
      </Notice>
    </ListPage>
  )
}
