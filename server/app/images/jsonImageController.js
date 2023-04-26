const {Photo, photosArray} = require("./model")

module.exports = {
    addPhoto: (photoData) => {
        const {album, originalName, url} = photoData
        const photo = new Photo(album, originalName, url)
        return JSON.stringify(photo)
    },
    getPhotos: () => {return JSON.stringify(photosArray)},
    getPhoto: (id) => {
        const photo = photosArray.find(item => {
            return item.id == id
        })
        return JSON.stringify(photo)
    },
    delPhoto: (id) => {
        const index = photosArray.findIndex(item => {
            return item.id == id
        })
        if(index == -1) return null
        const url = photosArray[index].getUrl();
        photosArray.splice(index, 1)
        return url
    },
    patchPhoto: (data) => {
        if(!data.id) return null
        const photo = photosArray.find(item => {
            return item.id == parseInt(data.id)
        })
        if(!photo) return null
        photo.update()
        return photo.getId()
    }
}