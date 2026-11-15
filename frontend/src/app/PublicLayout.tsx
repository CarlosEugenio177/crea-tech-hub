import type { ReactNode } from 'react'
import { Link, Outlet } from 'react-router'
import styles from './PublicLayout.module.css'

type PublicLayoutProps = { children?: ReactNode }

export function PublicLayout({ children }: PublicLayoutProps) {
  return (
    <>
      <header className={styles.header}>
        <Link to="/" className={styles.brand}>
          Mútua
        </Link>
      </header>
      <main className={styles.main}>{children ?? <Outlet />}</main>
    </>
  )
}
