import { ButtonLink } from '../../shared/ui/ButtonLink.tsx'
import { IllustratedPage } from '../../shared/ui/IllustratedPage.tsx'
import illustration from './assets/documento.svg'
import { RESERVE } from './reservationRoutes.ts'

export function TrackingPage() {
  return (
    <IllustratedPage
      eyebrow="Mútua · Sua solicitação"
      title="Acompanhe pelo link individual"
      description="Cada solicitação recebe um link individual, enviado com o comprovante. É por ele que você acompanha a análise ou cancela, sem login. O número do protocolo sozinho não dá acesso à solicitação."
      illustration={illustration}
      illustrationAlt="Pessoa ao lado de um documento"
    >
      <ButtonLink to={RESERVE}>Fazer uma solicitação</ButtonLink>
    </IllustratedPage>
  )
}
