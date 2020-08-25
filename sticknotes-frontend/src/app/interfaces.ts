import { Vector2 } from './utility/vector';
import { State } from './enums/state.enum';
import { UserRole } from './enums/user-role.enum';

export interface User {
  key: string;
  nickname: string;
  email: string;
  accessibleBoards: Board[];
}

export interface UserBoardRole {
  user: User;
  boardId: string;
  role: UserRole;
}

export interface Board {
  id: string;
  notes: Note[];
  users: UserBoardRole[];
  creationDate: string;
  title: string;
  creator: User;
  rows: number;
  cols: number;
  backgroundImg: string | null;
}

export interface NotePopupData {
  mode: State;
  noteData: { position: Vector2, boardId: string } | Note;
}

export interface CreateNoteApiData {
  content: string;
  image?: string;
  color: string;
  x: number;
  y: number;
  boardId: string;
}

export interface Note extends CreateNoteApiData {
  id: string;
  creationDate: string;
  creator: User;
}

/**
 * Except for "id", editable fields of the board
 */
export interface BoardUpdateData {
  id: string;
  title: string;
  rows: number;
  cols: number;
}

/**
 * Data used or displayed in the sidenav menu
 */
export interface SidenavBoardData extends BoardUpdateData {
  creationDate: string;
  backgroundImg: string | null;
}
