const bcrypt = require('bcryptjs');
const path = require('path')

class User{
    constructor(name, lastName, email, password){
        this.id = "u" + Date.now().toString()
        this.name = name
        this.lastName = lastName
        this.email = email
        this.password = password
        this.confirmed = false
        this.profilePicUrl = path.join(__dirname, "../../uploads/default_profile_pic.jpg")
        this.bio = ""
        usersArray.push(this)
    }
    verify = () => { this.confirmed = true }
}

let usersArray = []

module.exports = {User, usersArray}