import type { ReactNode } from 'react'
import { Link } from 'react-router'
import styles from './ButtonLink.module.css'

type ButtonLinkProps = {
  to: string
  children: ReactNode
}

export function ButtonLink({ to, children }: ButtonLinkProps) {
  return (
    <Link to={to} className={styles.button}>
      {children}
    </Link>
  )
}
