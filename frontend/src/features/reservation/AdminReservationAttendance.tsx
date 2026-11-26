import { Button } from '../../shared/ui/Button.tsx'
import { ProblemNotice } from '../../shared/ui/ProblemNotice.tsx'
import type { AdminReservationEntry } from './adminReservationsApi.ts'
import { useRecordAdminAttendance } from './adminReservationsApi.ts'
import styles from './AdminReservationPage.module.css'

export function AdminReservationAttendance({
  protocol,
  noShow,
}: Pick<AdminReservationEntry, 'protocol' | 'noShow'>) {
  const attendance = useRecordAdminAttendance(protocol)

  return (
    <div className={styles.attendance}>
      <Button
        variant="secondary"
        disabled={attendance.isPending}
        onClick={() => attendance.record(!noShow)}
      >
        {attendance.isPending
          ? 'Salvando…'
          : noShow
            ? 'Desfazer falta'
            : 'Registrar falta'}
      </Button>
      <ProblemNotice error={attendance.error} />
    </div>
  )
}
