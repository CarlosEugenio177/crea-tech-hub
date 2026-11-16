import { Button } from './Button.tsx'
import styles from './Pagination.module.css'

type PaginationProps = {
  page: number
  totalPages: number
  onChange: (page: number) => void
}

export function Pagination({ page, totalPages, onChange }: PaginationProps) {
  return (
    <nav className={styles.pagination} aria-label="Paginação">
      <Button
        variant="secondary"
        disabled={page === 0}
        onClick={() => onChange(page - 1)}
      >
        Anterior
      </Button>
      <p className={styles.position}>
        Página {page + 1} de {Math.max(totalPages, 1)}
      </p>
      <Button
        variant="secondary"
        disabled={page + 1 >= totalPages}
        onClick={() => onChange(page + 1)}
      >
        Próxima
      </Button>
    </nav>
  )
}
