class Tag{
    constructor(name, popularity){
        this.id = tagsArray.length
        this.name = name;
        this.popularity = popularity
        tagsArray.push(this)
    }
}

let tagsArray = []

module.exports = {Tag, tagsArray}