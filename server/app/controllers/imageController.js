const {Photo, photosArray} = require("../models/Photo")
const {getTag} = require("../controllers/tagsController")

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
        if(!photo) return JSON.stringify({error: "Photo not found"})
        return JSON.stringify(photo)
    },
    setLocation: (data) => {
        const {id, location} = data
        const photo = photosArray.find(item => {
            return item.id == id
        })
        if(!photo) return JSON.stringify({error: "Photo not found"})
        photo.location = location;
        return JSON.stringify(photo)
    },
    getPhotosFromFolder: (folder_name) => {
        const photos = photosArray.filter(i => {return i.album == folder_name})
        if(photos.length == 0) return JSON.stringify({error: "Photo not found"})
        else return JSON.stringify(photos)
    },
    getFilteredUrl: (id) => {
        const photo = photosArray.find(item => {
            return item.id == id
        })
        if(!photo) return JSON.stringify({error: "Photo not found"})
        return JSON.stringify(photo.filteredUrl)
    },
    delPhoto: (id) => {
        const index = photosArray.findIndex(item => {
            return item.id == id
        })
        if(index == -1) return JSON.stringify({error: "Photo not found"})
        const url = photosArray[index].url;
        photosArray.splice(index, 1)
        return JSON.stringify(url)
    },
    patchPhoto: (data) => {
        if(!data.id) return JSON.stringify({error: "Id not specified"})
        const photo = photosArray.find(item => {
            return item.id == parseInt(data.id)
        })
        if(!photo) return JSON.stringify({error: "Photo not found"})
        photo.update()
        return photo.id
    },
    applyFilterUpdate: (data) => {
        const {url, status , id} = data
        const photo = photosArray.find(item => {
            return item.id == parseInt(id)
        })
        if(!photo) return JSON.stringify({error: "Photo not found"})
        photo.update(status)
        return JSON.stringify(photo)
    },
    getTags: (id) => {
        const photo = photosArray.find(item => {
            return item.id == parseInt(id)
        })
        if(!photo || !id) return JSON.stringify({error: "Photo not found"})
        return JSON.stringify(photo.getTags())
    },
    addTag: (data) =>{
        const {photo_id, tag_id} = data;
        if(!photo_id || !tag_id) return JSON.stringify({error: "Required data not specified"})
        const tag = JSON.parse(getTag(tag_id))
        const photo = photosArray.find(item => {
            return item.id == parseInt(photo_id)
        })
        if(!photo || tag.error) return JSON.stringify({error: "Photo or tag not found"})
        photo.addTag(tag)
        tag.popularity += 1
        return JSON.stringify(photo)
    },
    addTags: (data) => {
        const {photo_id, tags} = data
        if(!photo_id || !tags || !Array.isArray(tags)) return JSON.stringify({error: "Required data not specified"})
        const photo = photosArray.find(item => {
            return item.id == parseInt(photo_id)
        })
        const tags_arr = tags.map(t => {
           return JSON.parse(getTag(t))
        })
        if(!photo || !tags_arr || tags_arr.length == 0 ) return JSON.stringify({error: "Photo or tag not found"})
        photo.addTags(tags_arr)
        for(tag in tags_arr){
            tag.popularity += 1
        }
        return JSON.stringify(photo)
    }
}