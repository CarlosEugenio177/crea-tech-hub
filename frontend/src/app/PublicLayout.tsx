import type { ReactNode } from 'react'
import { Link, NavLink, Outlet } from 'react-router'
import { ADMIN_LOGIN } from '../features/admin-auth/adminRoutes.ts'
import { RESERVE, TRACKING } from '../features/reservation/reservationRoutes.ts'
import { Brand } from '../shared/ui/Brand.tsx'
import styles from './PublicLayout.module.css'

type PublicLayoutProps = { children?: ReactNode }

const MENU = [
  { to: '/', label: 'Início' },
  { to: RESERVE, label: 'Solicitar espaço' },
  { to: TRACKING, label: 'Acompanhar solicitação' },
]

export function PublicLayout({ children }: PublicLayoutProps) {
  return (
    <>
      <header className={styles.header}>
        <div className={styles.bar}>
          <Brand to="/" />
          <nav className={styles.nav} aria-label="Principal">
            {MENU.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === '/'}
                className={({ isActive }) =>
                  isActive ? styles.activeItem : styles.item
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
        </div>
      </header>
      <main className={styles.main}>{children ?? <Outlet />}</main>
      <footer className={styles.footer}>
        <p>Mútua – Caixa de Assistência dos Profissionais do Crea</p>
        <Link to={ADMIN_LOGIN} className={styles.footerLink}>
          Acesso da equipe
        </Link>
      </footer>
    </>
  )
}
