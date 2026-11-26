import { Link } from 'react-router'
import negativeLogo from '../assets/logo-mutua-negativa.png'
import positiveLogo from '../assets/logo-mutua.png'
import styles from './Brand.module.css'

type BrandProps = { to: string; variant?: 'positive' | 'negative' }

export function Brand({ to, variant = 'negative' }: BrandProps) {
  return (
    <Link to={to} className={styles.brand}>
      <img
        className={styles.logo}
        src={variant === 'positive' ? positiveLogo : negativeLogo}
        width={640}
        height={180}
        alt="Mútua – Caixa de Assistência dos Profissionais do Crea"
      />
    </Link>
  )
}
