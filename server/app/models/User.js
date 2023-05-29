const bcrypt = require('bcryptjs');

class User{
    constructor(name, lastName, email, password){
        this.id = "u" + Date.now().toString()
        this.name = name
        this.lastName = lastName
        this.email = email
        this.password = password
        this.confirmed = false
        usersArray.push(this)
    }
    verify = () => { this.confirmed = true }
}

let usersArray = []

module.exports = {User, usersArray}