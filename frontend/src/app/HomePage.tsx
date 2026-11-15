import illustration from '../shared/assets/mulher-mesa.svg'
import { PageHeader } from '../shared/ui/PageHeader.tsx'
import styles from './IllustratedPage.module.css'

export function HomePage() {
  return (
    <div className={styles.page}>
      <PageHeader
        eyebrow="Mútua"
        title="Seu acesso ao Hub começa aqui"
        description="Solicite um espaço sem criar conta. Informe seus dados na própria solicitação."
      />
      <img
        className={styles.illustration}
        src={illustration}
        alt="Profissional trabalhando em uma mesa"
      />
    </div>
  )
}
