class Photo {
    constructor(album, originalName, url){
        this.id = Date.now()
        this.album = album;
        this.originalName = originalName;
        this.url = url;
        this.lastChange = 'original'
        this.history = [
            {
                "status": "original",
                "lastModifiedDate": this.id,
           }
        ]
        photosArray.push(this)
    }
    getUrl(){ return this.url }
    getId(){ return this.id }
    update(){
        this.history.push({
            "status": "change " + this.history.length.toString(),
            "lastModifiedDate": Date.now(),
        })
        this.lastChange = this.history[this.history.length - 1].status
    }
}

let photosArray = []

module.exports = {Photo, photosArray}