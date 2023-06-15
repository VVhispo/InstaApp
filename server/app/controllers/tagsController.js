const {Tag, tagsArray} = require("../models/Tag")
const fs = require("fs")
const path = require("path")

module.exports = {
    init: () => {
        const data = fs.readFileSync(path.join(__dirname,'../data/tags.json'));
        const tags = JSON.parse(data);
        for(let t in tags){
           new Tag(tags[t], 100);
        }
        console.log("Initialized tags from JSON")
    },
    getRawTags: () => {
        return JSON.stringify(tagsArray.map(t => {return t.name}))
    },
    getTags: () => {
        return JSON.stringify(tagsArray)
    },
    getTagsPopular: () => {
        return JSON.stringify(tagsArray.filter(t => {
            return t.popularity >= 100
        }))
    },
    getTag: (id) => {
        const tag = tagsArray.find(t => {return t.id == id})
        if(tag == undefined) return JSON.stringify({error: "Tag not found"})
        return JSON.stringify(tag)
    },
    addTag: (data) => {
        let {name, popularity} = data
        if(!popularity) popularity = 0
        if(!tagsArray.some(i => i.name == name)){
            const tag = new Tag(name, popularity)
            return JSON.stringify(tag)
        }
        const tag = tagsArray.find(t => {return t.name == name})
        tag.popularity += 1;
        return JSON.stringify(tag)
    }
}