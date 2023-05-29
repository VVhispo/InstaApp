const fs = require('fs');
const sharp = require("sharp");
const IC = require("./imageController")



module.exports = {
    getPhotoMeta: async(id) => {
        try {
            const photo = IC.getPhoto(id)
            if (!photo) return JSON.stringify({error: "Photo not found"})

            const url = JSON.parse(photo).url
            const metadata =  await sharp(url).metadata();
            return JSON.stringify(metadata)
        } catch (err) {
            return JSON.stringify({error: err.message})
        }
    },
    applyFilter: async(data) => {
        const {id, filterType, dimensions, tint} = data
        const photo = IC.getPhoto(id)
        if(!photo) return JSON.stringify({error: "Photo not found"})
        const url = JSON.parse(photo).url

        
        console.log(!dimensions.width)

        switch(filterType){
            case "resize":
                if(!dimensions || !dimensions.width || !dimensions.height) return JSON.stringify({error: "Dimensions not specified"})
                await sharp(url)
                    .resize({
                        width: dimensions.width,
                        height: dimensions.height
                    })
                    .toFile(url.slice(0,-4) + "_"+ filterType + ".jpg");
                break;
            case "tint":
                if(!tint || !tint.r || !tint.g || !tint.b) return JSON.stringify({error: "Color values not specified"})
                await sharp(url)
                .tint({
                    r:tint.r,
                    g:tint.g,
                    b:tint.b
                })
                    .toFile(url.slice(0,-4) + "_"+ filterType + ".jpg");
                break;
            case "negate":
                await sharp(url)
                    .negate()
                    .toFile(url.slice(0,-4) + "_"+ filterType + ".jpg");
                break;
            case "grayscale":
                await sharp(url)
                    .grayscale()
                    .toFile(url.slice(0,-4) + "_"+ filterType + ".jpg");
                break;
            default: return JSON.stringify({error: "Filter unknown"});
        }        
        const res = await IC.applyFilterUpdate({
            id: JSON.parse(photo).id,
            status: filterType,
            url: url.slice(0,-4) + "_"+ filterType + ".jpg"
        })
        return res
    }
}
