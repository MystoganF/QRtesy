export interface StaffUser {
  id: string;
  name: string;
  username: string;
  role: 'ORGANIZER' | 'DOOR_STAFF';
}

export interface LoginPayload {
  username: string;
  password: string;
}