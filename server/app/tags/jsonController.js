const {Tag, tagsArray} = require("./model")
const fs = require("fs")
const path = require("path")

module.exports = {
    init: () => {
        const data = fs.readFileSync(path.join(__dirname,'./tags.json'));
        const tags = JSON.parse(data);
        for(let t in tags){
           new Tag(tags[t], 0);
        }
        console.log("Initialized tags from JSON")
    },
    getRawTags: () => {
        return JSON.stringify(tagsArray.map(t => {return t.name}))
    },
    getTags: () => {
        return JSON.stringify(tagsArray)
    },
    getTag: (id) => {
        const tag = tagsArray.find(t => {return t.id == id})
        if(tag == undefined) return null
        return JSON.stringify(tag)
    },
    addTag: (data) => {
        const {name, popularity} = data
        if(!tagsArray.some(i => i.name == name)){
            const tag = new Tag(name, popularity)
            return JSON.stringify(tag)
        }
        return null;
    }
}