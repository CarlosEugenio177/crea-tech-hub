import illustration from '../shared/assets/binoculo-404.svg'
import { ButtonLink } from '../shared/ui/ButtonLink.tsx'
import { PageHeader } from '../shared/ui/PageHeader.tsx'
import styles from './IllustratedPage.module.css'

export function NotFoundPage() {
  return (
    <div className={styles.page}>
      <div className={styles.content}>
        <PageHeader
          eyebrow="Mútua · Erro 404"
          title="Este espaço não está disponível."
          description="A página pode ter sido movida ou o endereço está incorreto."
        />
        <ButtonLink to="/">Voltar ao início</ButtonLink>
      </div>
      <img
        className={styles.illustration}
        src={illustration}
        alt="Pessoa procurando com binóculo ao lado do número 404"
      />
    </div>
  )
}
