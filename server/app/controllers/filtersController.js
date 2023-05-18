const fs = require('fs');
const formidable = require('formidable');
const sharp = require("sharp");
const form = formidable({ multiples: true });
const IC = require("./imageController")

module.exports = {
    getPhotoMeta: async(id) => {
        return new Promise(async (resolve, reject) => {
            try {
                const photo = IC.getPhoto(id)
                if (photo) {
                    const url = JSON.parse(photo).url
                    const metadata =  await sharp(url).metadata();
                    resolve(metadata)
                }
                else resolve(null)
            } catch (err) {
                console.log(err.message)
                reject(null)
            }
        })
    },
    applyFilter: async(request) => {
        return new Promise(async (resolve, reject) => {
            form.parse(request, async function(err, fields) {
                if(err){
                    reject("err")
                }
                const photo = IC.getPhoto(fields.id)
                if(!photo) return null
                const url = JSON.parse(photo).url

                switch(fields.filterType){
                    case "resize":
                        if(fields.dimensions != null){
                            await sharp(url)
                                .resize({
                                    width: fields.dimensions.width,
                                    height: fields.dimensions.height
                                })
                                .toFile(url.slice(0,-4) + "_"+fields.filterType + ".jpg");
                        }
                        break;
                    case "tint":
                        if(fields.tint != null){
                            await sharp(url)
                            .tint({
                                r:fields.tint.r,
                                g:fields.tint.g,
                                b:fields.tint.b
                            })
                                .toFile(url.slice(0,-4) + "_"+fields.filterType + ".jpg");
                        }
                        break;
                    case "negate":
                        await sharp(url)
                            .negate()
                            .toFile(url.slice(0,-4) + "_"+fields.filterType + ".jpg");
                        break;
                    case "grayscale":
                        await sharp(url)
                            .grayscale()
                            .toFile(url.slice(0,-4) + "_"+fields.filterType + ".jpg");
                        break;
                    default: return null;
                }        
                const res = await IC.applyFilterUpdate({
                    id: JSON.parse(photo).id,
                    status: fields.filterType,
                    url: url.slice(0,-4) + "_"+fields.filterType + ".jpg"
                })
                resolve(res)
            })
        })
    }
}
