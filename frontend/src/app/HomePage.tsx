import { RESERVE } from '../features/reservation/reservationRoutes.ts'
import illustration from '../shared/assets/mulher-mesa.svg'
import { ButtonLink } from '../shared/ui/ButtonLink.tsx'
import { IllustratedPage } from '../shared/ui/IllustratedPage.tsx'
import styles from './HomePage.module.css'

const HOW_IT_WORKS = [
  {
    title: 'Escolha o espaço',
    text: 'Estação individual, sala de podcast ou mesa colaborativa.',
  },
  {
    title: 'Informe data, horário e seus dados',
    text: 'Sem criar conta, com início e fim no minuto que você precisar.',
  },
  {
    title: 'Aguarde a análise',
    text: 'A equipe da Mútua confere a solicitação e responde por WhatsApp e e-mail.',
  },
]

export function HomePage() {
  return (
    <>
      <IllustratedPage
        eyebrow="Mútua"
        title="Seu acesso ao Hub começa aqui"
        description="Solicite um espaço sem criar conta. Informe seus dados na própria solicitação."
        illustration={illustration}
        illustrationAlt="Profissional trabalhando em uma mesa"
      >
        <ButtonLink to={RESERVE}>Solicitar um espaço</ButtonLink>
      </IllustratedPage>
      <section className={styles.how} aria-labelledby="como-funciona">
        <h2 id="como-funciona" className={styles.heading}>
          Como funciona
        </h2>
        <ol className={styles.steps}>
          {HOW_IT_WORKS.map((step, index) => (
            <li key={step.title} className={styles.step}>
              <span className={styles.number}>{index + 1}</span>
              <h3 className={styles.title}>{step.title}</h3>
              <p className={styles.text}>{step.text}</p>
            </li>
          ))}
        </ol>
      </section>
    </>
  )
}
