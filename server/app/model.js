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
                "lastModifiedDate": Date.now(),
           }
        ]
        photosArray.push(this)
    }
}

let photosArray = []

module.exports = {Photo, photosArray}