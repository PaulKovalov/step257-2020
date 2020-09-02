import { Component, OnInit, Input } from '@angular/core';
import { UserService } from '../services/user.service';
import { BoardUsersApiService } from '../services/board-users-api.service';
import { UserBoardRole, User } from '../interfaces';
import { forkJoin } from 'rxjs';
import { UserRole } from '../enums/user-role.enum';
import { take } from 'rxjs/operators';
import { MatDialog } from '@angular/material/dialog';
import { AddUserComponent } from '../add-user/add-user.component';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.css']
})
export class UserListComponent implements OnInit {
  public usersWithRole: UserBoardRole[] = [];
  public currentUser: User;
  public currentUserRole: UserRole;
  @Input('boardId') public boardId: string;

  constructor(private userService: UserService,
    private boardUsersService: BoardUsersApiService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar) { }

  ngOnInit(): void {
    forkJoin(
      [
        this.userService.getUser().pipe(take(1)),
        this.boardUsersService.getBoardUsers(this.boardId)
      ]
    ).subscribe(([user, users]) => {
      this.currentUser = user;
      this.usersWithRole = users;
      const index = users.findIndex(userWithRole => user.id === userWithRole.user.id);
      if (index >= 0 && index < users.length) {
        this.currentUserRole = users[index].role;
      } else {
        this.currentUserRole = null;
      }
    });
  }

  canDelete(userBoardRole: UserBoardRole): boolean {
    if (this.currentUser && this.currentUserRole) {
      if (this.currentUserRole === UserRole.OWNER && this.currentUser.id !== userBoardRole.user.id) {
        return true;
      }
      if (this.currentUserRole === UserRole.ADMIN && userBoardRole.role === UserRole.USER) {
        return true;
      }
      return false;
    }
    return false;
  }

  openAddUserDialog(): void {
    const dialogRef = this.dialog.open(AddUserComponent, { data: { boardId: this.boardId } });
    dialogRef.afterClosed().subscribe(user => {
      if (user) {
        this.usersWithRole.push(user);
      }
    });
  }

  removeUser(userBoardRole: UserBoardRole): void {
    this.boardUsersService.removeUser(this.boardId, userBoardRole.id).subscribe(() => {
      this.snackBar.open("User removed successfully.", "Ok", {
        duration: 2000,
      });
      const indexOfRole = this.usersWithRole.indexOf(userBoardRole);
      if (indexOfRole >= 0 && indexOfRole < this.usersWithRole.length) {
        this.usersWithRole.splice(indexOfRole, 1);
      }
    }, err => {
      this.snackBar.open("Error occurred while removing user.", "Ok", {
        duration: 2000,
      });
    });
  }
}
