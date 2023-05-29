const {User, usersArray} = require('../models/User')
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

module.exports = {
    registerUser: async(data) => {
        const {name, lastName, email, password} = JSON.parse(data)
        const encrypted_pass = await bcrypt.hash(password, 10);
        const new_user = new User(name, lastName, email, encrypted_pass)
        const token = await jwt.sign(
            { user_id: new_user.id },
            process.env.JWT_KEY,
            { expiresIn: "60m" }
        );
        return JSON.stringify({token:token})
    },
    checkUser: (data) => {
        const {email} = JSON.parse(data)
        if(Object.values(JSON.parse(data)).filter(i => i.length == 0).length > 0){
            return JSON.stringify({error: 'Empty fields!'})
        }
        else if(usersArray.some(u => { return u.email == email })){
            return JSON.stringify({error: 'Account with that email already exists!'})
        }
        return null
    },
    getUsers: () => { return JSON.stringify(usersArray) },
    verifyUser: async(token) => {
        try {
            const decoded = await jwt.verify(token, process.env.JWT_KEY)
            const user = usersArray.find(u => { return u.id == decoded.user_id })
            if(!user) JSON.stringify({error: "User not found"})
            user.verify()
            return JSON.stringify({message: "Potwierdzono konto!"})
        }
        catch (ex) {
            return JSON.stringify({error: "Token expired!"})
        }
    },
    loginUser: async(data) => {
        const {email, password} = JSON.parse(data)
        if(!email || !password) return JSON.stringify({error: "Empty fields!"})
        const user = usersArray.find(u => { return u.email == email})
        if(!user) return JSON.stringify({error: "User not found!"})
        else if(!user.confirmed) return JSON.stringify({error: "You must verify your account first!"})
        const decrypted = await bcrypt.compare(password, user.password)
        if(!decrypted) return JSON.stringify({error: "Wrong password!"})
        const token = await jwt.sign(
            { user_id: user.id },
            process.env.JWT_KEY,
            { expiresIn: "365d" }
        );
        return JSON.stringify({token: token})
        
    }
}