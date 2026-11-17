import { useOutletContext } from 'react-router'
import type { components } from '../../shared/api/schema'

export type AdminAccount = components['schemas']['AdminAccount']
export type AdminPermission = components['schemas']['AdminPermission']

export function useAdminAccount() {
  return useOutletContext<AdminAccount>()
}
