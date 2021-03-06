window.Account or= {}

Account._loadUsersData = () ->
  Account.users or= new Account.Users
  Account.follows or= new Account.Follows
  Account.followed or= new Account.Followed

  Account.usersFiltersView = new Account.UsersFiltersView
    collection: Account.users
    follows: Account.follows
    followed: Account.followed
    refUser: Account.profile

  Account.follows.fetch complete: () ->
    Account.followed.fetch complete: () ->
      Account.users.fetch()

Account._loadProfileData = () ->
  usrParams = umappin.router.params ? id: Account.session.get 'id'
  Account.profile = new Account.User usrParams
  Account.readonly = umappin.router.params?
  Account.profileview = new Account.ProfileView model: Account.profile, readonly: Account.readonly
  Account.profile.fetch()

Account.loadProfileData = () ->
  requirejs [
    '/assets/js/common/models/picture_model.js'
    '/assets/js/lib/upclick.js'
  ], () ->
    requirejs ['/assets/js/account/models/user_model.js'], () ->
      requirejs ['/assets/js/common/views/picture_view.js'], () ->
        requirejs ['/assets/js/account/views/profile_view.js'], () ->
          Account._loadProfileData()

Account.loadUsersData = () ->
  if !Account.usersFiltersView
    requirejs [
      '/assets/js/account/collections/user_collection.js'
      '/assets/js/account/models/follow_model.js'
    ], () ->
      requirejs [
        '/assets/js/account/models/follows_model.js'
        '/assets/js/account/models/followed_model.js'
        '/assets/js/account/collections/follow_collection.js'
      ], () ->
        requirejs [
          '/assets/js/account/collections/follows_collection.js'
          '/assets/js/account/collections/followed_collection.js'
        ], () ->
          requirejs ['/assets/js/account/views/user_followed_view.js'], () ->
            requirejs ['/assets/js/account/views/user_follows_view.js'], () ->
              requirejs ['/assets/js/account/views/user_row_view.js'], () ->
                requirejs [
                  '/assets/js/account/views/users_filters_view.js'
                  '/assets/js/account/views/users_list_view.js'
                ], () ->
                  Account._loadUsersData()

Account._loadUsersData = () ->
  Account.users or= new Account.Users
  Account.follows or= new Account.Follows
  Account.followed or= new Account.Followed

  Account.usersFiltersView = new Account.UsersFiltersView
    collection: Account.users
    follows: Account.follows
    followed: Account.followed
    refUser: Account.profile

  Account.follows.fetch complete: () ->
    Account.followed.fetch complete: () ->
      Account.users.fetch()

Account.loadUserList = () ->
  requirejs [
    '/assets/js/common/models/picture_model.js'
    '/assets/js/lib/upclick.js'
  ], () ->
    requirejs ['/assets/js/account/models/user_model.js'], () ->
      requirejs ['/assets/js/common/views/picture_view.js'], () ->
        requirejs [
          '/assets/js/account/models/follow_model.js'
          '/assets/js/account/collections/user_collection.js'
        ], () ->
          requirejs [
            '/assets/js/account/models/usrfollows_model.js'
            '/assets/js/account/models/usrfollowed_model.js'
            '/assets/js/account/collections/follow_collection.js'
          ], () ->
            requirejs [
              '/assets/js/account/collections/userfollows_collection.js'
              '/assets/js/account/collections/userfollowed_collection.js'
              '/assets/js/account/collections/relateduser_collection.js'
            ], () ->
              requirejs ['/assets/js/account/views/user_followed_view.js'], () ->
                requirejs ['/assets/js/account/views/user_follows_view.js'], () ->
                  requirejs ['/assets/js/account/views/user_row_view.js'], () ->
                    requirejs [
                      '/assets/js/account/views/users_filters_view.js'
                      '/assets/js/account/views/users_list_view.js'
                    ], () ->
                      Account._loadUserList()

Account._loadUserList = () ->
  Account.profile or= new Account.User(Account.session)

  Account.userfollows = new Account.UserFollows
  Account.userfollowed = new Account.UserFollowed
  Account.relatedusers = new Account.RelatedUsers

  Account.usersFiltersView = new Account.UsersFiltersView
    collection: Account.relatedusers
    follows: Account.userfollows
    followed: Account.userfollowed
    refUser: Account.profile

  Account.userfollows.fetch complete: () ->
    Account.userfollowed.fetch complete: () ->
      Account.relatedusers.fetch()

Account.init = () ->
  Account.usersFiltersView = null
  Account.loadProfileData()
