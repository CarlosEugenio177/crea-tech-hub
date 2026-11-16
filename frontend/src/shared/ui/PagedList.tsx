import type { ReactNode } from 'react'
import { problemMessage } from '../api/problem'
import { Notice } from './Notice.tsx'
import styles from './PagedList.module.css'
import { Pagination } from './Pagination.tsx'

type PagedListProps<Item> = {
  data: { items: Item[]; totalPages: number } | undefined
  error: unknown
  page: number
  emptyText: string
  onPageChange: (page: number) => void
  children: (item: Item) => ReactNode
}

export function PagedList<Item>({
  data,
  error,
  page,
  emptyText,
  onPageChange,
  children,
}: PagedListProps<Item>) {
  if (error) return <Notice tone="error">{problemMessage(error)}</Notice>
  if (!data) return <Notice tone="info">Carregando…</Notice>

  return (
    <>
      {data.items.length === 0 ? (
        <p className={styles.empty}>{emptyText}</p>
      ) : (
        <ul className={styles.list}>
          {data.items.map((item) => children(item))}
        </ul>
      )}
      {(data.totalPages > 1 || page > 0) && (
        <Pagination
          page={page}
          totalPages={data.totalPages}
          onChange={onPageChange}
        />
      )}
    </>
  )
}
