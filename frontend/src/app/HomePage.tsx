import { RESERVE } from '../features/reservation/reservationRoutes.ts'
import { ButtonLink } from '../shared/ui/ButtonLink.tsx'
import styles from './HomePage.module.css'

export function HomePage() {
  return (
    <section className={styles.home}>
      <h1>Reserve uma sala ou estação</h1>
      <p>
        Consulte os horários do Hub e envie o pedido para a equipe da Mútua.
      </p>
      <ButtonLink to={RESERVE}>Solicitar um espaço</ButtonLink>
    </section>
  )
}
