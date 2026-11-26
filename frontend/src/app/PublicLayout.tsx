import type { ReactNode } from 'react'
import { Link, NavLink, Outlet, useMatch } from 'react-router'
import { ADMIN_LOGIN } from '../features/admin-auth/adminRoutes.ts'
import { RESERVE, TRACKING } from '../features/reservation/reservationRoutes.ts'
import { Brand } from '../shared/ui/Brand.tsx'
import styles from './PublicLayout.module.css'

type PublicLayoutProps = { children?: ReactNode }

const MENU = [
  { to: RESERVE, label: 'Reservar' },
  { to: TRACKING, label: 'Acompanhar' },
]

export function PublicLayout({ children }: PublicLayoutProps) {
  const isHome = useMatch('/') !== null

  return (
    <div className={styles.site}>
      <header className={styles.header}>
        <div className={styles.bar}>
          <Brand to="/" />
          <nav className={styles.nav} aria-label="Principal">
            {MENU.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  isActive || (isHome && item.to === RESERVE)
                    ? styles.activeItem
                    : styles.item
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
    </div>
  )
}
