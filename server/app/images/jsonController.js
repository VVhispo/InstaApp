const {Photo, photosArray} = require("./model")
const {getTag} = require("../tags/jsonController")

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
        const url = photosArray[index].url;
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
        return photo.id
    },
    getTags: (id) => {
        const photo = photosArray.find(item => {
            return item.id == parseInt(id)
        })
        if(!photo || !id) return null;
        return JSON.stringify(photo.getTags())
    },
    addTag: (data) =>{
        const {photo_id, tag_id} = data;
        if(!photo_id || !tag_id) return null
        const tag = JSON.parse(getTag(tag_id))
        const photo = photosArray.find(item => {
            return item.id == parseInt(photo_id)
        })
        if(!photo || !tag) return null
        photo.addTag(tag)
        return JSON.stringify(photo)
    },
    addTags: (data) => {
        const {photo_id, tags} = data
        if(!photo_id || !tags || !Array.isArray(tags)) return null
        const photo = photosArray.find(item => {
            return item.id == parseInt(photo_id)
        })
        const tags_arr = tags.map(t => {
            return JSON.parse(getTag(t))
        })
        if(!photo || !tags_arr || tags_arr.length == 0) return null
        photo.addTags(tags_arr)
        return JSON.stringify(photo)
    }
}