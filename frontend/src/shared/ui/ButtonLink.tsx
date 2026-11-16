import type { ReactNode } from 'react'
import { Link } from 'react-router'
import styles from './Button.module.css'

type ButtonLinkProps = {
  to: string
  variant?: 'primary' | 'secondary'
  children: ReactNode
}

export function ButtonLink({
  to,
  variant = 'primary',
  children,
}: ButtonLinkProps) {
  return (
    <Link to={to} className={`${styles.button} ${styles[variant]}`}>
      {children}
    </Link>
  )
}
