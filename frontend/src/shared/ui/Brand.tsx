import { Link } from 'react-router'
import logo from '../assets/logo-mutua-negativa.png'
import styles from './Brand.module.css'

type BrandProps = { to: string }

export function Brand({ to }: BrandProps) {
  return (
    <Link to={to} className={styles.brand}>
      <img
        className={styles.logo}
        src={logo}
        width={640}
        height={180}
        alt="Mútua – Caixa de Assistência dos Profissionais do Crea"
      />
    </Link>
  )
}
