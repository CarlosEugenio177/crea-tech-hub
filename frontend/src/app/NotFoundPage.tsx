import illustration from '../shared/assets/binoculo-404.svg'
import { ButtonLink } from '../shared/ui/ButtonLink.tsx'
import { IllustratedPage } from '../shared/ui/IllustratedPage.tsx'

export function NotFoundPage() {
  return (
    <IllustratedPage
      eyebrow="Mútua · Erro 404"
      title="Este espaço não está disponível."
      description="A página pode ter sido movida ou o endereço está incorreto."
      illustration={illustration}
      illustrationAlt="Pessoa procurando com binóculo ao lado do número 404"
    >
      <ButtonLink to="/">Voltar ao início</ButtonLink>
    </IllustratedPage>
  )
}
