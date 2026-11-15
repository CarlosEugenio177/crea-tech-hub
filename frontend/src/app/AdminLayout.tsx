import { NavLink, Outlet } from 'react-router'
import { useLogout } from '../features/admin-auth/adminAuthApi.ts'
import {
  ADMIN_HOME,
  ADMIN_PASSWORD,
} from '../features/admin-auth/adminRoutes.ts'
import type { components } from '../shared/api/schema'
import styles from './AdminLayout.module.css'

type AdminLayoutProps = { account: components['schemas']['AdminAccount'] }

const MENU = [
  { to: ADMIN_HOME, label: 'Visão geral' },
  { to: ADMIN_PASSWORD, label: 'Alterar senha' },
]

export function AdminLayout({ account }: AdminLayoutProps) {
  const logout = useLogout()

  return (
    <div className={styles.shell}>
      <aside className={styles.sidebar}>
        <p className={styles.brand}>Mútua</p>
        <nav className={styles.nav} aria-label="Administração">
          <p className={styles.navLabel}>Administração</p>
          {MENU.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end
              className={({ isActive }) =>
                isActive ? styles.activeItem : styles.item
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className={styles.account}>
          <p className={styles.accountName}>{account.fullName}</p>
          <p className={styles.accountEmail}>{account.email}</p>
          <button
            type="button"
            className={styles.logout}
            onClick={() => logout.mutate({})}
          >
            Sair
          </button>
        </div>
      </aside>
      <main className={styles.content}>
        <Outlet context={account} />
      </main>
    </div>
  )
}
