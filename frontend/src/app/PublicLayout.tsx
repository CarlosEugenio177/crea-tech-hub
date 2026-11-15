import { Link, Outlet } from 'react-router'
import styles from './PublicLayout.module.css'

export function PublicLayout() {
  return (
    <>
      <header className={styles.header}>
        <Link to="/" className={styles.brand}>
          Mútua
        </Link>
      </header>
      <main className={styles.main}>
        <Outlet />
      </main>
    </>
  )
}
