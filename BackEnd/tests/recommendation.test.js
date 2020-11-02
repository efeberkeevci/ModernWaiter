const app = require('../backend_server')
const supertest = require('supertest')
const request = supertest(app)
const recommendation_logic = require('../recommendation_logic')

// describe('getItemRecommendation()', async done => {
//     test('Should 200', () => {
//       // Arrange
//       //const mock = jest.fn().mockReturnValue({itemId : 1})
//       //recommendation_logic.getRecommendation = mock

//       // Act
//       const response = request.get('/recommendation/1/1')

//       // Assert
//       expect(response.status).toBe(200)
//       done()
//     });
//   });

  it('gets the test endpoint', async done => {
    const response = await request.get('/recommendation/1/1')
  
    expect(response.status).toBe(200)
    //expect(response.body.message).toBe('pass!')
    done()
  })