import { Link } from 'react-router'
import { RESERVE, TRACKING } from '../features/reservation/reservationRoutes.ts'
import { ButtonLink } from '../shared/ui/ButtonLink.tsx'
import styles from './HomePage.module.css'

export function HomePage() {
  return (
    <div className={styles.home}>
      <section className={styles.intro}>
        <h1>Reserve uma sala ou estação</h1>
        <p>
          Consulte a disponibilidade e solicite uma reserva no Crea Tech Hub.
        </p>
        <ButtonLink to={RESERVE}>Solicitar um espaço</ButtonLink>
      </section>
      <section className={styles.tracking}>
        <h2>Já fez um pedido?</h2>
        <Link to={TRACKING}>Consultar minha reserva</Link>
      </section>
    </div>
  )
}
