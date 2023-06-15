const path = require("path")

class Photo {
    constructor(album, originalName, url){
        this.id = Date.now()
        this.album = album;
        this.originalName = originalName;
        this.url = url;
        this.lastChange = 'original'
        this.filteredUrl
        this.thumbnailUrl
        this.history = [
            {
                "status": "original",
                "lastModifiedDate": this.id,
           }
        ]
        this.tags = []
        photosArray.push(this)
    }
    update(status = null){
        if(!this.filteredUrl) this.filteredUrl = path.join(this.url.split(".")[0] + "_filter.jpg")
        if(!status){
            this.history.push({
                "status": "change " + this.history.length.toString(),
                "lastModifiedDate": Date.now(),
            }) 
        }else{
            this.history.push({
                "status": status,
                "lastModifiedDate": Date.now(),
            })
        }
        this.lastChange = this.history[this.history.length - 1].lastModifiedDate
        
    }
    addTag(tag){
        if(!this.tags.some(t =>  t.id == tag.id)) this.tags.push(tag)
    }
    addTags(tags){
        for(let t in tags){
            if(!this.tags.some(i =>  i.id == tags[t].id)) this.tags.push(tags[t])
        }
    }
    getTags(){
        return {
            id: this.id,
            tags: this.tags
        }
    }
}

let photosArray = []

module.exports = {Photo, photosArray}