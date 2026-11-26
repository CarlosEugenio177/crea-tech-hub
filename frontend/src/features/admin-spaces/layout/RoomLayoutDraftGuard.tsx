import { useEffect, useId, useRef } from 'react'
import { useBlocker } from 'react-router'
import { Button } from '../../../shared/ui/Button.tsx'
import styles from './RoomLayout.module.css'

export function RoomLayoutDraftGuard({ dirty }: { dirty: boolean }) {
  const blocker = useBlocker(
    ({ currentLocation, nextLocation }) =>
      dirty && currentLocation.pathname !== nextLocation.pathname,
  )
  const dialogRef = useRef<HTMLDialogElement>(null)
  const titleId = useId()
  const descriptionId = useId()
  const blocked = blocker.state === 'blocked'

  useEffect(() => {
    if (!dirty) return
    const warn = (event: BeforeUnloadEvent) => event.preventDefault()
    window.addEventListener('beforeunload', warn)
    return () => window.removeEventListener('beforeunload', warn)
  }, [dirty])

  useEffect(() => {
    if (blocked) dialogRef.current?.showModal()
  }, [blocked])

  if (blocker.state !== 'blocked') return null

  const stay = () => {
    dialogRef.current?.close()
    blocker.reset()
  }

  return (
    <dialog
      ref={dialogRef}
      className={styles.leaveDialog}
      aria-labelledby={titleId}
      aria-describedby={descriptionId}
      onCancel={(event) => {
        event.preventDefault()
        stay()
      }}
    >
      <h2 id={titleId}>Sair sem salvar o mapa?</h2>
      <p id={descriptionId}>
        As alterações do mapa serão perdidas se você sair.
      </p>
      <div className={styles.leaveActions}>
        <Button onClick={stay}>Continuar editando</Button>
        <Button variant="secondary" onClick={blocker.proceed}>
          Descartar e sair
        </Button>
      </div>
    </dialog>
  )
}
